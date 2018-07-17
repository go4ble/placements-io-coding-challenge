package models

import models.GetLineItemsRequest._
import play.api.mvc.QueryStringBindable

import scala.collection.mutable

case class GetLineItemsRequest(pageOffset: Option[Int] = None,
                               pageSize: Option[Int] = None,
                               sortBy: Option[String] = None,
                               sortDirection: Option[String] = None,
                               campaignIds: Seq[Int] = Nil,
                               export: Boolean = false) {
  pageOffset foreach { po =>
    require(po >= 0, "pageOffset must be a positive value")
  }
  pageSize foreach { ps =>
    require(PAGE_SIZES contains ps, s"pageSize must be one of ${PAGE_SIZES.mkString("[", ",", "]")}")
  }
  sortBy foreach { sb =>
    require(SORT_BY_OPTIONS contains sb, s"sortBy must be one of ${SORT_BY_OPTIONS.mkString("[", ",", "]")}")
  }
  sortDirection foreach { sd =>
    require(Set("asc", "desc") contains sd, "sortDirection must be either 'asc' or 'desc'")
  }

  def getPageOffset: Int       = this.pageOffset    getOrElse DEFAULT_PAGE_OFFSET
  def getPageSize: Int         = this.pageSize      getOrElse DEFAULT_PAGE_SIZE
  def getSortBy: String        = this.sortBy        getOrElse DEFAULT_SORT_BY
  def getSortDirection: String = this.sortDirection getOrElse DEFAULT_SORT_DIRECTION

  // helper to guard from negative pageOffset
  def pageFrom(offset: Int): GetLineItemsRequest = {
    val newOffset = Math.max(getPageOffset + offset, 0)
    this.copy(pageOffset = Some(newOffset))
  }

  def toggleSort(sortBy: String): GetLineItemsRequest = {
    if (sortBy == getSortBy) {
      val direction = if (getSortDirection == SORT_ASC) SORT_DESC else SORT_ASC
      this.copy(sortDirection = Some(direction), pageOffset = None)
    } else {
      this.copy(sortBy = Some(sortBy), sortDirection = None, pageOffset = None)
    }
  }

  // https://gist.github.com/kottenator/9d936eb3e4e3c3e02598
  def pagination(total: Int): Seq[Option[Int]] = {
    val range = mutable.ListBuffer[Int]()
    val rangeWithDots = mutable.ListBuffer[Option[Int]]()
    val last = total / getPageSize
    val delta = 2
    val current = getPageOffset + 1
    val left = current - delta
    val right = current + delta + 1

    for (i <- 1 to last if i == 1 || i == last || i >= left && i < right)
      range += i

    var lo: Option[Int] = None
    for (i <- range) {
      for (l <- lo) {
        if (i - l == 2) {
          rangeWithDots += Some(l + 1)
        } else if (i - l != 1) {
          rangeWithDots += None
        }
      }
      rangeWithDots += Some(i)
      lo = Some(i)
    }

    rangeWithDots
  }
}

object GetLineItemsRequest {
  val PAGE_SIZES = Seq(10, 50, 100)

  val SORT_BY_LINE_ITEM_ID   = "sbLineItemId"
  val SORT_BY_LINE_ITEM_NAME = "sbLineItemName"
  val SORT_BY_CAMPAIGN_ID    = "sbCampaignId"
  val SORT_BY_CAMPAIGN_NAME  = "sbCampaignName"
  val SORT_BY_BOOKED_AMOUNT  = "sbBookedAmount"
  val SORT_BY_ACTUAL_AMOUNT  = "sbActualAmount"
  val SORT_BY_ADJUSTMENTS    = "sbAdjustments"
  val SORT_BY_BILLABLE       = "sbBillable"
  val SORT_BY_OPTIONS = Seq(SORT_BY_LINE_ITEM_ID, SORT_BY_LINE_ITEM_NAME, SORT_BY_CAMPAIGN_ID, SORT_BY_CAMPAIGN_NAME,
    SORT_BY_BOOKED_AMOUNT, SORT_BY_ACTUAL_AMOUNT, SORT_BY_ADJUSTMENTS, SORT_BY_BILLABLE)

  val SORT_ASC  = "asc"
  val SORT_DESC = "desc"

  val DEFAULT_PAGE_OFFSET = 0
  val DEFAULT_PAGE_SIZE = 10
  val DEFAULT_SORT_BY: String = SORT_BY_LINE_ITEM_ID
  val DEFAULT_SORT_DIRECTION: String = SORT_ASC

  type IntBinder = QueryStringBindable[Int]
  type IntSeqBinder = QueryStringBindable[Seq[Int]]
  type StringBinder = QueryStringBindable[String]
  type QueryParams = Map[String, Seq[String]]
  implicit def queryStringBindable(implicit intBinder: IntBinder, intSeqBinder: IntSeqBinder,
                                   stringBinder: StringBinder): QueryStringBindable[GetLineItemsRequest] =
    new QueryStringBindable[GetLineItemsRequest] {

      private def optionBinder[T](key: String)(implicit params: QueryParams, binder: QueryStringBindable[T]): Option[T] =
        binder.bind(key, params).flatMap(_.toOption)

      private def optionUnbinder[T](key: String, value: Option[T])(implicit binder: QueryStringBindable[T]): Option[String] =
        value.map(v => binder.unbind(key, v))

      override def bind(key: String, params: QueryParams): Option[Either[String, GetLineItemsRequest]] = {
        implicit val queryParams: QueryParams = params
        val pageOffset    = optionBinder[Int]("pageOffset")
        val pageSize      = optionBinder[Int]("pageSize")
        val sortBy        = optionBinder[String]("sortBy")
        val sortDirection = optionBinder[String]("sortDirection")
        val campaignIds   = optionBinder[Seq[Int]]("campaignId").getOrElse(Nil)

        Some(Right(GetLineItemsRequest(pageOffset, pageSize, sortBy, sortDirection, campaignIds)))
      }

      override def unbind(key: String, value: GetLineItemsRequest): String = Seq(
        optionUnbinder("pageOffset", value.pageOffset),
        optionUnbinder("pageSize", value.pageSize),
        optionUnbinder("sortBy", value.sortBy),
        optionUnbinder("sortDirection", value.sortDirection),
        if (value.campaignIds.isEmpty) None else Some(intSeqBinder.unbind("campaignId", value.campaignIds))
      ).flatten.mkString("&")
    }
}
