Related issue: https://github.com/haproxy/haproxy/issues/2592

The repro steps consist of:
  - tomcat10h2c - simple java application, powered by spring Boot 3.3, embedded Tomcat 10
  - jetty11h2c - simple java application, powered by spring Boot 3.1, embedded Jetty 11
  - jetty12h2c - simple java application, powered by spring Boot 3.3, embedded Jetty 12
  - repro-haproxy - haproxy, configured to connect to the above applications via h2c

Step 1. Start applications:
  [console1] $ ./tomcat10h2c/mvnw -f tomcat10h2c/pom.xml clean spring-boot:run
  [console2] $ ./jetty11h2c/mvnw -f jetty11h2c/pom.xml clean spring-boot:run
  [console3] $ ./jetty12h2c/mvnw -f jetty12h2c/pom.xml clean spring-boot:run

Step 2. Start repro-haproxy:
  [console3] $ bash generate-test-certificate.sh
  [console3] $ docker build -t repro-haproxy:latest repro-haproxy
  [console3] $ docker run --name issue-haproxy --net host --rm repro-haproxy:latest

Step 3. Make sure that standalone app works
  - tomcat10h2c
    $ curl -w "\n" --http1.1 "http://localhost:20000/" # {"response":"Hello World"}
    $ curl -w "\n" --http2   "http://localhost:20000/" # {"response":"Hello World"}

  - jetty11h2c
    $ curl -w "\n" --http1.1 "http://localhost:20001/" # {"response":"Hello World"}
    $ curl -w "\n" --http2   "http://localhost:20001/" # {"response":"Hello World"}

  - jetty12h2c
    $ curl -w "\n" --http1.1 "http://localhost:20002/" # {"response":"Hello World"}
    $ curl -w "\n" --http2   "http://localhost:20002/" # {"response":"Hello World"}

Step 4. Test the operation of the entire flow:
  - tomcat10h2c
    $ curl -w "\n" --http1.1 -k "https://localhost:30000/" # {"response":"Hello World"}
      Logs, warning is emitted:
        2024-06-04T16:20:02.736+02:00  WARN 664004 --- [io-20000-exec-5] org.apache.coyote.http2.Http2Parser      : Connection [2], An unknown setting with identifier [8] and value [1] was ignored

    $ curl -w "\n" --http2 -k   "https://localhost:30000/" # {"response":"Hello World"}

  - jetty11h2c
    $ curl -w "\n" --http1.1 -k "https://localhost:30001/" # {"response":"Hello World"}
    $ curl -w "\n" --http2 -k   "https://localhost:30001/" # {"response":"Hello World"}

  - jetty12h2c
    $ curl -w "\n" --http1.1 -k "https://localhost:30002/" # HTTP ERROR 400 Invalid Authority
      Wireshark output, sniffed haproxy->simple app connection:
        HyperText Transfer Protocol 2
            Stream: HEADERS, Stream ID: 1, Length 37, GET /
                ...
                [Header Length: 124]
                [Header Count: 6]
                Header: :method: GET
                Header: :scheme: https
                Header: :path: /
                Header: host: localhost:30002
                Header: user-agent: curl/8.5.0
                Header: accept: */*
                [Response in frame: 6234]

    $ curl -w "\n" --http2 -k   "https://localhost:30002/" # {"response":"Hello World"}
      Wireshark output, sniffed haproxy->simple app connection:
        HyperText Transfer Protocol 2
            Stream: HEADERS, Stream ID: 1, Length 37, GET /
                ...
                [Header Length: 130]
                [Header Count: 6]
                Header: :method: GET
                Header: :scheme: https
                Header: :authority: localhost:30002
                Header: :path: /
                Header: user-agent: curl/8.5.0
                Header: accept: */*
                [Full request URI: https://localhost:30002/]
                [Response in frame: 10471]

