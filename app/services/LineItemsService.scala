package services

import javax.inject.Inject
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import GetLineItemsRequest._

import scala.concurrent.{ExecutionContext, Future}

class LineItemsService @Inject()
(protected val dbConfigProvider: DatabaseConfigProvider)
(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  private def baseQuery(implicit request: GetLineItemsRequest) = {
    val query = Campaigns.table.joinLeft(LineItems.table).on(_.id === _.campaignId)
    if (request.campaignIds.isEmpty)
      query
    else
      query.filter(_._1.id inSet request.campaignIds)
  }

  def getLineItems(implicit request: GetLineItemsRequest): Future[Seq[(Campaign, Option[LineItem])]] = {
    val sorted = setSort(baseQuery)
    val trimmed = if (request.export)
      sorted
    else
      sorted.drop(request.getPageOffset * request.getPageSize).take(request.getPageSize)
    val statement = trimmed.result.statements.head
    db run trimmed.result
  }

  def countLineItems(implicit request: GetLineItemsRequest): Future[Int] = {
    val query = baseQuery.length
    db run query.result
  }

  def grandTotal(implicit request: GetLineItemsRequest): Future[BigDecimal] = {
    val query = baseQuery.map { case (_, lineItemOption) =>
      lineItemOption.map(lineItem => lineItem.actualAmount + lineItem.adjustments)
    }.sum
    db.run(query.result).map(_.getOrElse(0))
  }

  def getCampaigns: Future[Seq[(Campaign, Int)]] = {
    val subQuery = LineItems.table.groupBy(_.campaignId).map {
      case (campaignId, lineItems) => (campaignId, lineItems.length)
    }
    val query = Campaigns.table.joinLeft(subQuery).on(_.id === _._1).map {
      case (campaigns, counts) => (campaigns, counts.map(_._2).getOrElse(0))
    }.sortBy {
      case (campaign, count) => (count.desc, campaign.name.asc)
    }
    db run query.result
  }

  type CampaignLineItemQuery = Query[(Campaigns, Rep[Option[LineItems]]), (Campaign, Option[LineItem]), Seq]
  // TODO find a cleaner approach to this method
  // issue is that the implicit functions asc and desc are difficult to apply generically the various Rep types
  private def setSort(query: CampaignLineItemQuery)(implicit request: GetLineItemsRequest) =
    request.getSortBy match {
      case SORT_BY_LINE_ITEM_ID   => query.sortBy { case (_, lineItems) =>
        val rep = lineItems.map(_.id)
        if (request.getSortDirection == SORT_ASC) rep.asc else rep.desc
      }
      case SORT_BY_LINE_ITEM_NAME => query.sortBy { case (_, lineItems) =>
        val rep = lineItems.map(_.name)
        if (request.getSortDirection == SORT_ASC) rep.asc else rep.desc
      }
      case SORT_BY_CAMPAIGN_ID    => query.sortBy { case (campaigns, _) =>
        val rep = campaigns.id
        if (request.getSortDirection == SORT_ASC) rep.asc else rep.desc
      }
      case SORT_BY_CAMPAIGN_NAME  => query.sortBy { case (campaigns, _) =>
        val rep = campaigns.name
        if (request.getSortDirection == SORT_ASC) rep.asc else rep.desc
      }
      case SORT_BY_BOOKED_AMOUNT  => query.sortBy { case (_, lineItems) =>
        val rep = lineItems.map(_.bookedAmount)
        if (request.getSortDirection == SORT_ASC) rep.asc else rep.desc
      }
      case SORT_BY_ACTUAL_AMOUNT  => query.sortBy { case (_, lineItems) =>
        val rep = lineItems.map(_.actualAmount)
        if (request.getSortDirection == SORT_ASC) rep.asc else rep.desc
      }
      case SORT_BY_ADJUSTMENTS    => query.sortBy { case (_, lineItems) =>
        val rep = lineItems.map(_.adjustments)
        if (request.getSortDirection == SORT_ASC) rep.asc else rep.desc
      }
      case SORT_BY_BILLABLE       => query.sortBy { case (_, lineItems) =>
        val rep = lineItems.map(lineItem => lineItem.actualAmount + lineItem.adjustments)
        if (request.getSortDirection == SORT_ASC) rep.asc else rep.desc
      }
    }
}
