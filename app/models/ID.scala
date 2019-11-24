package models

/**
  * Domain model of id
  * @param userID ユーザID
  * @param taskID タスクID
  */
case class ID(userID: Int, taskID: Int)

object ID extends DomainModel[ID] {
  import slick.jdbc.GetResult
  implicit def getResult: GetResult[ID] = GetResult(
    r => ID(r.nextInt, r.nextInt)
  )
}
