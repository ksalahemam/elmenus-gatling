package default

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class RecordedSimulation extends Simulation {


  // Variables Decleration
  val pauseInterval = 2
  val runCount = 1

  // DEBUG CODE - DONT DELETE
  //    .check(bodyString.saveAs("myresponseId"))).exec(session => {
  //    val maybeId = session.get("myresponseId").asOption[String]
  //    println(maybeId.getOrElse("COULD create order"))
  //    session
  //  })

  // stg 01 variables
  val token = "Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCIsInVzZXJUeXBlIjoiZmYifQ.eyJzdWIiOiIxYWE2NmU1NC0yMjI0LTExZTgtOTI0ZS0wMjQyYWMxMTAwMTEiLCJpc3MiOiJlbG1lbnVzLmNvbSIsImV4cCI6MTUyNzM0NzUyNSwiaWF0IjoxNTI0NzU1NTI1fQ.-tMyIBKgsE_PStbkS9AITRFw8GE0g_KME10c7e2MwZlCpApYC149C5YWV7lQAJznVUWLsf7W7Ci0xvtA7BG98w"
  val cityUUID = "35185821-2224-11e8-924e-0242ac110011"
  val itemSizeUUID = "866c5a28-00af-4a9c-b6fc-ec75aef06acd"
  val branchUUID = "cf469454-f021-4f3c-875a-5db81ff30b7f"
  val userAddressUUID = "c488616f-eee1-4bdd-b34a-91b351f1db37"

//  // dev 02 variables
//  val token = "Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCIsInVzZXJUeXBlIjoiZmYifQ.eyJzdWIiOiIwM2NmMDQ2MS0yMjE5LTExZTgtYmFiNy0wMjQyYWMxMTAwMDYiLCJpc3MiOiJlbG1lbnVzLmNvbSIsImV4cCI6MTUyNzMzMzk2OCwiaWF0IjoxNTI0NzQxOTY4fQ.cYysfirRt3velHal5JcGUpX5J3Hffy08t9SZpxK4khFx2Z4TU_thqyoPRmuVi6heF5aG8RpWi4KXBgSLL5lUeg"
//  val itemSizeUUID = "ce318b42-2219-11e8-bab7-0242ac110006"
//  val branchUUID = "19a9cb13-2219-11e8-bab7-0242ac110006"
//  val cityUUID = "17201d0e-2219-11e8-bab7-0242ac110006"
//  val userAddressUUID = "ebfdd283-08a2-4e2b-9d51-9445b8533c57"

  val httpProtocol = http
		.baseURL("http://stg01.elmenus.com:4001/2.0")
		.proxy(Proxy("stg01.elmenus.com", 4001).httpsPort(443))
		.inferHtmlResources()
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.headers(
			Map(
				"X-Client-Id" -> "0417b8e7-0f3f-11e8-87cc-0242ac110002",
				"content-type" -> "application/json",
				"Authorization" -> token
			))

	val scn = scenario("RecordedSimulation")
		.exec(http("abdo kofta")
			.get("/restaurant/vyv3"))
  	.pause(pauseInterval)
			.exec(http("jamba")
			.get("/restaurant/dg27"))
		.pause(pauseInterval)
		.exec(http("lookups")
			.get("/lookups"))
		.pause(pauseInterval)
		.exec(http("profile")
			.get("/profile"))
		.pause(pauseInterval)
		.exec(http("order")
			.post("/order")
			.body(StringBody(s"""{"items":[{"quantity":5,"itemSizeUUID": "$itemSizeUUID","comment":"","extras":[]}],"branchUUID":"$branchUUID","userAddressUUID":"$userAddressUUID"}"""))
      .check(jsonPath("$.order.uuid").saveAs("orderUUID"))
    )
    .exec(session => {
        val orderUUID = session.get("orderUUID").as[String]
        println(s"created order with uuid $orderUUID")
        session
      })
    .exec(http("cancel that order")
      .put("/order/${orderUUID}/cancel?time=1525104827585")
      .body(StringBody("""{"reason":"CANCELED_CHANGED_MY_MIND","comment":""}"""))
    )
    .pause(pauseInterval)
		.exec(http("authentication")
			.post("/auth")
			.body(StringBody("""{"email": "admin@elmenus.com", "password": "V5bcQAYb"}"""))
    )
		.pause(pauseInterval)
    .exec(http("list restaurants")
			.get("/restaurant")
    )
		.pause(pauseInterval)
    .exec(http("search restaurants")
			.get(s"/restaurant/search?q=burger&city=$cityUUID")
		)
		.pause(pauseInterval)
    .exec(http("list notifications")
			.get(s"/notification")
		)
		.pause(pauseInterval)


	setUp(scn.inject(atOnceUsers(runCount))).protocols(httpProtocol)
}
