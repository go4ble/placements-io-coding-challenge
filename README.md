# Placements.io Coding Challenge

## Running the application
**Note:** This project is setup to run with Docker. Having Docker installed will make running the app and tests much
more simple. Please see [About Docker CE](https://docs.docker.com/install/) for information regarding installation or
upgrading.

#### Starting
1. Run the following from within the top level directory:
`$ docker-compose up`
2. Visit the running application in your browser at [localhost:9000/invoice](http://localhost:9000/invoice).
3. The application starts with an empty database. To populate it, click on the _import_ button and select the sample
   file under the /data directory.

#### Stopping / cleaning up
1. Press `Ctrl-C` to stop running containers.
2. Run `docker-compose down -v` to clean up any leftovers.

#### Running tests
1. Run the following from within the top level directory:
`$ docker-compose -f test.docker-compose.yml up`
2. This will execute `sbt test` within a fresh docker container, so it may take a little while as dependencies are
downloaded and the project is compiled.

#### Already have _sbt_ installed?
You can avoid using Docker and execute `sbt run`. Be sure to update conf/application.conf with appropriate database configs.

## Chosen features
