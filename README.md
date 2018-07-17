# Placements.io Coding Challenge

## Running the application
**Note:** This project is setup to run with Docker. Having Docker installed will make running the app and tests much
more simple. Please see [About Docker CE](https://docs.docker.com/install/) for information regarding installation or
upgrading.

#### Starting
1. Run the following from within the top level directory: `docker-compose up`
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
You can avoid using Docker and execute `sbt run`. Be sure to update conf/application.conf with appropriate database
configs.

## Chosen features
As the request was to spend only a few hours on the challenge, I chose features that I felt comfortable implementing
such that I could complete them in the time frame.

I chose to implement this project with the Play Framework in Scala for it is a framework I just recently learned, but
had yet to try it on any particular topic. Normally for a project like this, I would create a separate UI or Single Page
Application in jQuery or AngularJS with a RESTful service backing it; however, I felt that writing two applications
would not be a good use of time.

In the end, the features I finally implemented could have all existed in just a SPA with no backing service, but I had
anticipated potentially getting to multi-user features, which would require a centralized data source.

### Features

* The user should be able to browse through the line-item data
  * implemented via pagination
  * [pagination logic](https://github.com/go4ble/placements-interview/blob/master/app/models/GetLineItemsRequest.scala#L48)
  * [pagination view](https://github.com/go4ble/placements-interview/blob/master/app/views/_pagination.scala.html)
  * there is an outstanding bug with pagination and small sets of data (i.e. filtered). This [unit test](https://github.com/go4ble/placements-interview/blob/master/test/models/GetLineItemsRequestSpec.scala#L19)
    currently fails, but demonstrates the issue. With more time, I would have liked to have fixed it.
* The user should be able to see each line-items billable amount
* The user should be able to see the invoice grand-total
  * [sql](https://github.com/go4ble/placements-interview/blob/master/app/services/LineItemsService.scala#L40)
* The user should be able to sort the data
  * [model logic](https://github.com/go4ble/placements-interview/blob/master/app/models/GetLineItemsRequest.scala#L38)
  * [view logic](https://github.com/go4ble/placements-interview/blob/master/app/views/_sortable.scala.html)
  * [sql](https://github.com/go4ble/placements-interview/blob/master/app/services/LineItemsService.scala#L59)
* The user should be able to output the invoice to *.CSV, *.XLS, etc.
  * [controller logic](https://github.com/go4ble/placements-interview/blob/master/app/controllers/HomeController.scala#L55)
  * Implemented CSV export within the controller. With more time, would have liked to have extracted it into its own
    service.
  * Normally, I would have used an existing library to implement CSV generation. However, I also felt that learning such
    a library would have taken more time than just creating something simple and untested (for the sake of this project).
* The user should be able to filter the data
  * Affects grand-total and export results

### Additional features
In addition to the features listed in the challenge prompt, I implemented a couple others that I felt could potentially
add to the usability of the application

* File import
  * Allows import of the provided sample data, or any other data that matches its shape.
  * Only inserts records with new IDs
  * Does not perform any friendly content validation
* Column details on hover
  * Some of the cells contain large amounts of information, but my display is kinda small.
  * So I thought I'd try an idea to have an entire column grow when the mouse hovers.
  * In the end, I'm not a fan of the affect; particularly with how everything seems to jump around. However, I decided
    to leave it in for now to see what others thought or how they reacted.
