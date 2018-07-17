package models

import org.scalatestplus.play._

class GetLineItemsRequestSpec extends PlaySpec {

  "GetLineItemsRequest pagination" should {

    "generate a sequence of pages" in {
      def withParameters(pageOffset: Int, pageSize: Int, total: Int) =
        GetLineItemsRequest(pageOffset = Some(pageOffset), pageSize = Some(pageSize)).pagination(total).map {
          case Some(n) => n.toString
          case None => "..."
        }.mkString("[", ",", "]")

      withParameters(0, 10, 200)  mustBe "[1,2,3,...,20]"
      withParameters(5, 10, 200)  mustBe "[1,...,4,5,6,7,8,...,20]"
      withParameters(19, 10, 200) mustBe "[1,...,18,19,20]"
      withParameters(0, 10, 11)   mustBe "[1,2]"
    }
  }

  "GetLineItemsRequest parameter binding" should {

    "unbinds to a parameter string" in {
      val bindable = GetLineItemsRequest.queryStringBindable
      bindable.unbind("", GetLineItemsRequest(campaignIds = Seq(380))) mustBe "campaignId=380"
      bindable.unbind("", GetLineItemsRequest(campaignIds = Seq(380, 381))) mustBe "campaignId=380&campaignId=381"
    }

  }
}
