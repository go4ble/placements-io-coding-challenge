package models

import slick.jdbc.PostgresProfile.api._

case class Campaign(id: Option[Int], name: String) {
  def displayName: String = {
    val split = name.split(':').map(_.trim)
    val company = split.head
    val desc = split.tail.headOption.map(d => d.split(' ').head)
    desc.map(d => s"$company : $d ...").getOrElse(company)
  }
}

class Campaigns(tag: Tag) extends Table[Campaign](tag, "campaigns") {
  def id = column[Int]("id", O.PrimaryKey)
  def name = column[String]("name")
  def * = (id.?, name) <> (Campaign.tupled, Campaign.unapply)
}

object Campaigns {
  val table = TableQuery[Campaigns]
}
