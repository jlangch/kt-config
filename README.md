kt-config
=========

Utility for reading configuration files. Configuration settings can be defined using dot and/or scoped notation.


## Overview

 - implemented in plain Kotlin with no dependencies
 - can load from string, files, URLs, or classpath
 - supports dot notation for names and nested sections
 - override properties with Java system properties and environment variables
 - global substitutions
 
 
## Example

```
def host = "foo.org"

# Common section
common {
  user = "john.doe"
}

# Database section
db {
  h2 {
    driver = "org.h2.Driver"
    url = "jdbc:h2:tcp://${host}:9567/~data/db;MVCC=TRUE"
  }
  
  postgresql {
    driver="org.postgresql.Driver"
    url = "jdbc:postgresql://${host}:5432/serviceplanet"
  }
}
```

##### Dot notation

```
# Common section
common.user = "john.doe"

# Test section
test.host = "foo.org"
test.port = "8000"
```

##### Mixed notation

```
# Common section
common.user = "john.doe"

# Test section
test {
  host = "foo.org"
  port = "8000"

  # Database section
  db {
    # H2
    h2 {
      driver = "org.h2.Driver"
      url = "jdbc:h2:tcp://localhost:9567/~data/db;MVCC=TRUE"
    }
  
    #PostgreSQL
    postgresql {
      driver="org.postgresql.Driver"
      url = "jdbc:postgresql://localhost:5432/serviceplanet"
    }
  }
}
```


##### Using Definitions

```
# Definitions favor DRY. Definitions can be refereneced as
# ${name}.
# All environment variables are available as implicit definitions
# prefixed with 'env.' e.g. 'env.HOME'. 
# Java system properties are available as implicit definitions
# prefixed with 'system.' e.g 'system.io.temp' 

# define the port
def port = "8000"
def database = "abc.db"


app {
  dir {
    home = "${env.HOME}"         # uses Environment variable 'HOME'
    temp = "${system.io.temp}"   # uses Java system property 'io.temp'
  }
}

db {
  h2 {
    driver = "org.h2.Driver"
    url = "jdbc:h2:tcp://localhost:${port}/~data/${database}"
  }
  postgresql {
    driver="org.postgresql.Driver"
    url = "jdbc:postgresql://localhost:${port}/${database}"
  }
}
```


## Usage

The `ConfigReader` can be created with a configuration `String` or by
passing an `InputStream`. It returns always configuration values in
a flat _dot_ notation (e.g. _db.h2.port = 8000_). The parsed `Config` 
object can be converted to a `Map<String,String>` or a `Properties` 
object optionally.

#### Examples
```kotlin
val config: Config = ConfigReader("....").read()
```

```kotlin
val config: Map<String,String> = ConfigReader("....").read().toMap()
```

```kotlin
val config: Properties = ConfigReader("....").read().toProperties()
```

The configuration:
```
def port = "8000"
def database = "sample.db"

db {
  postgresql {
    driver="org.postgresql.Driver"
    url = "jdbc:postgresql://localhost:${port}/${database}"
  }
}
```

Returns two configuration values:

- db.postgresql.driver -> org.postgresql.Driver
- db.postgresql.url -> jdbc:postgresql://localhost:8000/sample.db



#### Selecting sub configurations

```
# Common section
common.user = "john.doe"

# Test section
test {
  host = "foo.org"
  port = "8000"
}

# UAT section
uat {
  host = "foo.org"
  port = "8001"
}
```


```kotlin
val config: Config = ConfigReader("....").read().getSubConfig("common", "test")
```

Returns three configuration values:

- user -> john.doe
- host -> foo.org
- port -> 8001


#### Passing user definitions

The _definitions_ in the config file may be overridden 
programmatically:

```
def port = "8000"
def database = "abc.db"

db {
  postgresql {
    driver="org.postgresql.Driver"
    url = "jdbc:postgresql://localhost:${port}/${database}"
  }
}
```

```kotlin
val config: Config = ConfigReader("....", hashMapOf("port" to "8080")).read()
```

## Error handling

The `ConfigReader` throws a `ConfigurationException` whenever 
the configuration is not conform to the specification or a definition
reference can not be resolved. The exception provides the caller 
with an error message and a position (row, col) of the error. 