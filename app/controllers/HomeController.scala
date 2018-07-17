package controllers

import javax.inject._
import models.GetLineItemsRequest
import play.api.libs.Files
import play.api.mvc._
import play.mvc.Http.HeaderNames
import services.{FileUploadService, LineItemsService}

import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()
(cc: ControllerComponents, fileUpload: FileUploadService, lineItemsService: LineItemsService)
(implicit ec: ExecutionContext) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def upload(params: GetLineItemsRequest): Action[MultipartFormData[Files.TemporaryFile]] =
    Action(parse.multipartFormData) async { request =>
      request.body.file("data") map { data =>
        fileUpload.save(data.ref.getAbsolutePath) map { count =>
          Redirect(routes.HomeController.invoice(params))
            .flashing("uploadNotice" -> s"Successfully imported $count records")
        }
      } getOrElse {
        Future.successful(BadRequest("Missing file"))
      }
    }

  def invoice(implicit params: GetLineItemsRequest): Action[AnyContent] = Action async { implicit request =>
    for {
      count      <- lineItemsService.countLineItems
      lineItems  <- lineItemsService.getLineItems
      grandTotal <- lineItemsService.grandTotal
      campaigns  <- lineItemsService.getCampaigns
    } yield {
      Ok(views.html.invoice(lineItems, count, grandTotal, campaigns, params))
    }
  }

  def exportCsv(params: GetLineItemsRequest): Action[AnyContent] = Action async { request =>
    val csvHeader = Seq("id","name","campaign_id","campaign_name","booked_amount","actual_amount","adjustments","billable_amount")
    val sb: StringBuilder = new StringBuilder(csvHeader.mkString("", ",", "\n"))
    def csvEscape(value: String) = s""""${value.replaceAll("\"", "\"\"")}""""
    val adjustParams = GetLineItemsRequest(
      export = true,
      campaignIds = params.campaignIds
    )
    lineItemsService.getLineItems(adjustParams).map { lineItems =>
      for ((campaign, Some(lineItem)) <- lineItems) {
        val csvLine: Seq[String] = Seq(lineItem.id.get.toString, csvEscape(lineItem.name), campaign.id.get.toString,
          csvEscape(campaign.name), lineItem.bookedAmount.toString, lineItem.actualAmount.toString,
          lineItem.adjustments.toString, lineItem.billableAmount.toString)
        sb ++= csvLine.mkString("", ",", "\n")
      }
      Ok(sb.mkString).as("text/csv")
        .withHeaders(HeaderNames.CONTENT_DISPOSITION -> "attachment; filename=export.csv")
    }
  }
}
