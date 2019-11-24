package models

import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext
import play.api.db.slick.{DatabaseConfigProvider => DBConfigProvider}

/**
  * task テーブルへの Accessor
  */
@Singleton
class Tasks @Inject()(dbcp: DBConfigProvider)(implicit ec: ExecutionContext) extends Dao(dbcp) {

  import profile.api._
  import utility.Await

  val table = "task"

  /**
    * DB上に保存されている全てのタスクを取得する
    * @return
    */
  def list: Seq[Task] = Await.result(
    db.run(sql"SELECT id, title, description, is_done, created_at FROM #$table".as[Task])
  )

  /**
    * DB上に保存されているタスクの中からIDが一致するものを取得する
    * @param id
    * @return
    */
  def findByID(id: Int): Option[Task] = Await.result(
    db.run(sql"SELECT id, title, description, is_done, created_at FROM #$table WHERE id=#$id".as[Task].headOption)
  )

  def findTask(id: Int): Task = Await.result(
    db.run(sql"SELECT id, title, description, is_done, created_at FROM #$table WHERE id=#$id".as[Task].head)
  )

  /**
    * DB上に保存されているタスクの中から完了状態が一致するもの全てを取得する
    * @param isDone
    * @return
    */
  def findByIsDone(isDone: Boolean): Seq[Task] = Await.result(
    db.run(
      sql"SELECT id, title, description, is_done, created_at FROM #$table WHERE is_done=#$isDone"
        .as[Task]
    )
  )

  /**
    * DB上に保存されているタスクの中からタスク名と完了状態が一致するものを取得する
    * @param title
    * @param isDone
    * @return
    */
  def search(title: String, isDone: Boolean): Seq[Task] = Await.result(
    db.run(
      sql"SELECT id, title, description, is_done, created_at FROM #$table WHERE title='#$title' AND is_done=#$isDone"
        .as[Task]
    )
  )

  /**
    * DBにタスクを保存する
    * @param task
    * @return
    */
  def save(task: Task): Int = task match {
    case Task(0, title, description, isDone, _) =>
      Await.result(
        db.run(sqlu"INSERT INTO #$table (title, description, is_done) VALUES ('#$title', '#$description', '#$isDone')")
      )
    case Task(id, title, description, isDone, _) =>
      Await.result(
        db.run(
          sqlu"UPDATE #$table SET title='#$title', description='#$description', is_done='#$isDone' WHERE id = #$id"
        )
      )
  }

  /**
    * DB上に保存されているタスクの中からIDが一致するものを削除する
    * @param id
    * @return
    */
  def delete(id: Int): Int = Await.result(
    db.run(sqlu"DELETE FROM #$table WHERE id = #$id")
  )

}
