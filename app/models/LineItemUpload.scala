package models

import play.api.libs.json._

case class LineItemUpload(id: Int,                   // {"id": 1,
                          campaignId: Int,           //  "campaign_id": 1,
                          campaignName: String,      //  "campaign_name": "Satterfield-Turcotte : Multi-channelled next generation analyzer - e550",
                          lineItemName: String,      //  "line_item_name": "Awesome Plastic Car - 6475",
                          bookedAmount: BigDecimal,  //  "booked_amount": 430706.6871532752,
                          actualAmount: BigDecimal,  //  "actual_amount": 401966.50504006835,
                          adjustments:  BigDecimal) {//  "adjustments": 1311.0731142230268 }
  def toLineItem = LineItem(Some(id), campaignId, lineItemName, bookedAmount, actualAmount, adjustments)
}

object LineItemUpload {
  implicit val jsonConfiguration: JsonConfiguration = JsonConfiguration(JsonNaming.SnakeCase)
  implicit val lineItemUploadReads: Reads[LineItemUpload] = Json.reads[LineItemUpload]
}
