kt-config
=========

Utility for reading configuration files. Configuration settings can be defined using dot and/or scoped notation.


## Examples


#### Scoped notation

```
# Common section
common {
  user = "john.doe"
}

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

##### Dot notation

```
# Common section
common.user = "john.doe"

# Test section
test.host = "foo.org"
test.port = "8000"

# UAT section
uat.host = "foo.org"
uat.port = "8001"
```

##### Mixed notation

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

##### Nested sections

```
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
```

_this is equivalent to:_

```
# H2
db.h2 {
  driver = "org.h2.Driver"
  url = "jdbc:h2:tcp://localhost:9567/~data/db;MVCC=TRUE"
}

#PostgreSQL
db.postgresql.driver="org.postgresql.Driver"
db.postgresql.url = "jdbc:postgresql://localhost:5432/serviceplanet"
```

##### Using Definitions

```
# Definitions favor DRY. Definitions can be refereneced as
# ${name}.

# define the port
def port = "8000"
def database = "abc.db"

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
def database = "abc.db"

db {
  postgresql {
    driver="org.postgresql.Driver"
    url = "jdbc:postgresql://localhost:${port}/${database}"
  }
}
```

Returns the two configuration values:

- db.postgresql.driver -> org.postgresql.Driver
- db.postgresql.url -> jdbc:postgresql://localhost:8000/abc.db


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

#### Selecting sub configurations



## Error handling

The `ConfigReader` throws a `ConfigurationException` whenever 
the configuration is not conform to the specification or a definition
reference can not be resolved. The exception provides the caller 
with an error message and a position (row, col) of the error. 