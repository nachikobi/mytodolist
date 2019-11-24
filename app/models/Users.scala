package models

import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext
import play.api.db.slick.{DatabaseConfigProvider => DBConfigProvider}

/**
  * user dao
  */
@Singleton
class Users @Inject()(dbcp: DBConfigProvider)(implicit ec: ExecutionContext) extends Dao(dbcp) {

  import profile.api._
  import utility.Await

  val table = "user"

  /**
    * DB上からユーザ名とパスワードが一致するものを取得し、idを返す
    * ログインに活用
    * @param name
    * @param password
    * @return
    */
  def login(name: String, password: String): Option[Int] = Await.result(
    db.run(sql"SELECT id FROM #$table WHERE name='#$name' AND password='#$password'".as[Int].headOption)
  )

  def list: Seq[User] = Await.result(
    db.run(sql"SELECT id, name, password, created_at FROM #$table".as[User])
  )

  /**
    * DB上からユーザ名が一致するものを取得する
    * すでに登録されているユーザ名と同じユーザ名で新規登録できないようにするため
    * @param name
    * @return
    */
  def findByName(name: String): Option[User] = Await.result(
    db.run(sql"SELECT FROM #$table WHERE name='#$name'".as[User].headOption)
  )

  def getName(id: Int): String = Await.result(
    db.run(sql"SELECT name FROM #$table WHERE id='#$id'".as[String].head)
  )

  /**
    * DB上からユーザ名とパスワードが一致するものを取得する
    * ユーザ情報を更新するために使う
    * @param name
    * @param password
    * @return
    */
  def checkUser(id: Int, name: String, password: String): Option[User] = Await.result(
    db.run(
      sql"SELECT id, name, password, created_at FROM #$table WHERE id=#$id AND name='#$name' AND password='#$password'"
        .as[User]
        .headOption
    )
  )

  /**
    * DBにユーザを保存する
    * @param user
    * @return
    */
  def save(user: User): Int = user match {
    case User(0, name, password, _) =>
      Await.result(
        db.run(sqlu"INSERT INTO #$table (name, password) VALUES ('#$name', '#$password')")
      )
    case User(id, name, password, _) =>
      Await.result(
        db.run(
          sqlu"UPDATE #$table SET password='#$password' WHERE id = #$id AND name = '#$name'"
        )
      )
  }

  /**
    * 退会作業(DBからユーザを削除する)
    * @param id
    * @return
    */
  def delete(id: Int): Int = Await.result(
    db.run(sqlu"DELETE FROM #$table WHERE id = #$id")
  )
}
