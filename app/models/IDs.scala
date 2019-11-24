package models

import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext
import play.api.db.slick.{DatabaseConfigProvider => DBConfigProvider}

/**
  * user dao
  */
@Singleton
class IDs @Inject()(dbcp: DBConfigProvider)(implicit ec: ExecutionContext) extends Dao(dbcp) {

  import profile.api._
  import utility.Await

  val table = "id"

  def list: Seq[ID] = Await.result(
    db.run(sql"SELECT userID, taskID FROM #$table".as[ID])
  )

  /**
    * DB上からuserIDに紐づいたタスクのtaskIDを取得する
    * @param userID
    * @return
    */
  def findByUserID(userID: Int): Seq[Int] = Await.result(
    db.run(sql"SELECT taskID FROM #$table WHERE userID='#$userID'".as[Int])
  )

  /**
    * DBにユーザIDとタスクIDの組を保存する
    * @param userID
    * @param taskID
    * @return
    */
  def save(userID: Int, taskID: Int): Int = Await.result(
    db.run(sqlu"INSERT INTO #$table (userID, taskID) VALUES ('#$userID', '#$taskID')")
  )

  /**
    * DBからユーザIDとタスクIDが一致するものを削除する
    * @param taskID
    * @return
    */
  def delete(taskID: Int): Int = Await.result(
    db.run(sqlu"DELETE FROM #$table WHERE taskID=#$taskID")
  )

  /**
    * 退会作業(DBからユーザIDが一致するものを削除)
    * @param id
    * @return
    */
  def deleteByUser(id: Int): Int = Await.result(
    db.run(sqlu"DELETE FROM #$table WHERE userID = #$id")
  )
}
