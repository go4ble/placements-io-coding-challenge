package models

import slick.jdbc.PostgresProfile.api._

case class LineItem(id: Option[Int],
                    campaignId: Int,
                    name: String,
                    bookedAmount: BigDecimal,
                    actualAmount: BigDecimal,
                    adjustments: BigDecimal) {
  def displayName: String = name.split('-').head.trim
  def billableAmount: BigDecimal = actualAmount + adjustments
}

class LineItems(tag: Tag) extends Table[LineItem](tag, "line_items") {
  def id = column[Int]("id", O.PrimaryKey)
  def campaignId = column[Int]("campaign_id")
  def name = column[String]("name")
  def bookedAmount = column[BigDecimal]("booked_amount")
  def actualAmount = column[BigDecimal]("actual_amount")
  def adjustments = column[BigDecimal]("adjustments")
  def * = (id.?, campaignId, name, bookedAmount, actualAmount, adjustments) <> (LineItem.tupled, LineItem.unapply)
}

object LineItems {
  val table = TableQuery[LineItems]
}
