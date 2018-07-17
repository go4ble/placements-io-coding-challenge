package services

import javax.inject.Inject
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json._
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

class FileUploadService @Inject()
(protected val dbConfigProvider: DatabaseConfigProvider)
(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  def save(filename: String): Future[Int] = {
    val json = Json parse Source.fromFile(filename).mkString
    json.validate[Seq[LineItemUpload]] match {
      case JsSuccess(lineItems, _) =>
        saveLineItems(lineItems)
      case JsError(errors) =>
        println(errors)
        Future.failed(JsResultException(errors))
    }
  }

  private def saveLineItems(lineItems: Seq[LineItemUpload]): Future[Int] = {
    val campaigns = lineItems.groupBy { item => Campaign(Some(item.campaignId), item.campaignName) }.keySet
    require(campaigns.size == campaigns.flatMap(_.id).size, "campaign id / name combinations should be unique")

    val items = lineItems.map(_.toLineItem)

    db.run(for {
      // insert only new campaigns and line items by comparing with preexisting records
      existingCampaigns <- Campaigns.table.map(_.id).result
      campaignsInserted <- Campaigns.table ++= campaigns.filter(_.id.exists(!existingCampaigns.contains(_)))
      existingItems     <- LineItems.table.map(_.id).result
      itemsInserted     <- LineItems.table ++= items.filter(_.id.exists(!existingItems.contains(_)))
    } yield sum(campaignsInserted, itemsInserted))
  }

  private def sum(a: Option[Int], b: Option[Int]) = a.flatMap(x => b.map(y => x + y)).getOrElse(0)
}
