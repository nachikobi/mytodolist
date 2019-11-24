package models

import java.sql.Timestamp

/**
  * Domain model of enquete
  * @param id        ID
  * @param name      ユーザ名
  * @param password  パスワード
  * @param createdAt 登録日時
  */
case class User(id: Int, name: String, password: String, createdAt: Timestamp)

object User extends DomainModel[User] {
  import slick.jdbc.GetResult
  implicit def getResult: GetResult[User] = GetResult(
    r => User(r.nextInt, r.nextString, r.nextString, r.nextTimestamp)
  )

  def apply(name: String, password: String): User = User(0, name, password, null)
}
