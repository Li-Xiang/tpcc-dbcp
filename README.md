## TPC-C for Apache DBCP

TPC-C for Apache DBCP is a TPC-C benchmark (workload generator), which base on JDBC and Apache DBCP.

### Features
- Multiple relationship database support: MySQL, PostgreSQL, Oracle, DB2, MSSQL, Derby(Java DB), SQLite. 
- Faster TPC-C data generate (compatible with tpcc-mysql), by using multi-threads,  batch insert and defer foreign key create.

### Install

Download the pre-built binary here and then unzip it. it requires minimum of Java 8 at runtime.

### Usage
```
$ ./tpcc.sh -h
usage: tpcc [options] [command]

options:
 -d,--datasource <name>     Specify datasource name which define in "dsConfig.json" config fine, default is
                            'default-data-source'.
 -h,--help                  Print this help and exit.
 -i,--report-interval <#>   Periodically report intermediate statistics with a specified interval in seconds. set to 0
                            disables intermediate reports
 -l,--log-level <level>     Set the tpcc benchmark's logging level {ALL|TRACE|DEBUG|INFO|WARN|ERROR|FATAL|OFF}.
 -m,--measure-time <#>      Total execution time in seconds. default 3600 seconds
 -r,--warmup <#>            Rampup(warmup) time in seconds, default 10 seconds.
 -t,--threads <#>           Benchmark worker threads.
 -w,--warehouses <#>        Specifies the number of warehouse, default is tpcc.warehouse table's rows number.

command:
  The command is an optional argument, the following commands are understood:
     load: Generate the test data for TPC-C benchmarks.
    check: Check the test data for TPC-C benchmarks.
     run : Runs the actual TPC-C test. (default)

examples:
  tpcc --datasource mysql-ds-1 --warehouses 10 --threads 20 load
  tpcc --datasource mysql-ds-1 check
  tpcc --datasource mysql-ds-1 --warehouses 10 --warmup 60 --threads 10 --log-level error --measure-time 3600 --report-interval 10 run
```

### Get Started

You can start TPC-C test by reference "deploy_reference.txt", which under tpcc-dbcp's schema directory.

### Build
To build the jar files, you must use minimum version of Java 8 with Apache ant.

```
$ git clone https://github.com/Li-Xiang/tpcc-dbcp.git
$ cd tpcc-dbcp
$ ant clean
$ ant

```





