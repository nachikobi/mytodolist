package controllers

import javax.inject.{Inject, Singleton}
import models.{ID, IDs, Task, Tasks, User, Users}
import play.api.mvc.{AbstractController, ControllerComponents, Result}
import utility.Digest

/**
  * TodoListコントローラ
  */
@Singleton
class TodoListController @Inject()(users: Users, tasks: Tasks, ids: IDs)(cc: ControllerComponents)
    extends AbstractController(cc) {

  /**
    * インデックスページを表示
    */
  def index = Action { implicit request =>
    // 200 OK ステータスで app/views/index.scala.html をレンダリングする
    Ok(views.html.index("Welcome to Play application!"))
  }

  /**
    * ログインページを表示
    * @return
    */
  def login = Action { request =>
    Ok(views.html.login(request))
  }

  def loginCheck = Action { request =>
    (for {
      param    <- request.body.asFormUrlEncoded
      name     <- param.get("name").flatMap(_.headOption)
      password <- param.get("password").flatMap(_.headOption)
    } yield {
      val pass = Digest.apply(password)
      users.login(name, pass) match {
        case Some(e) =>
          Redirect(routes.TodoListController.list(e)).withNewSession
        case None => NotFound(s"ユーザ名かパスワードが違います")
      }
    }).getOrElse[Result](Redirect("/todolist/login"))
  }

  /**
    * ユーザ新規登録ページを表示
    * @return
    */
  def userRegister = Action { request =>
    Ok(views.html.userCreate(request))
  }

  def userConfirm = Action { request =>
    (for {
      param    <- request.body.asFormUrlEncoded
      name     <- param.get("name").flatMap(_.headOption)
      password <- param.get("password").flatMap(_.headOption)
    } yield {
      users.findByName(name) match {
        case Some(e) => NotFound(s"すでに登録されているユーザ名のため、登録できません。別のユーザ名にしてください。")
        case None =>
          val pass = Digest.apply(password)
          users.save(User(name, pass))
          val last = users.list.last
          Redirect("/todolist/" + last.id + "/tasks").withNewSession
      }
    }).getOrElse[Result](Redirect("/todolist/login"))
  }

  /**
    * ユーザーパスワードの変更
    * @param userID
    * @return
    */
  def userEdit(userID: Int) = Action { request =>
    Ok(views.html.userEdit(request, userID))
  }

  def userUpdate(userID: Int) = Action { request =>
    (for {
      param   <- request.body.asFormUrlEncoded
      name    <- param.get("name").flatMap(_.headOption)
      oldPass <- param.get("oldPass").flatMap(_.headOption)
      newPass <- param.get("newPass").flatMap(_.headOption)
    } yield {
      val older = Digest.apply(oldPass)
      users.checkUser(userID, name, older) match {
        case Some(e) =>
          val newer = Digest.apply(newPass)
          users.save(User(userID, name, newer, null))
          Redirect("/todolist/" + userID + "/tasks").withNewSession
        case None => NotFound(s"ユーザ名かパスワードが違います")
      }
    }).getOrElse[Result](Redirect("todolist/" + userID + "userEdit"))
  }

  def leave(userID: Int) = Action { request =>
    users.delete(userID)
    ids.deleteByUser(userID)
    Ok(views.html.leave(request))
  }

  /**
    * タスク一覧を表示
    * @return
    */
  def list(userID: Int) = Action { request =>
    val entries  = tasks.list
    val userName = users.getName(userID)
    val taskIDs  = ids.findByUserID(userID) //Seq[Int]を返す
    Ok(views.html.list(request, entries, userID, userName, taskIDs))
  }

  def entry(userID: Int, id: Int) = Action {
    tasks.findByID(id) match {
      case Some(e) => Ok(views.html.entry(e, userID))
      case None    => NotFound(s"No entry for id=${id}")
    }
  }

  def comp(userID: Int, id: Int) = Action {
    tasks.findByID(id) match {
      case Some(e) =>
        var comped = e
        if (e.isDone == false) {
          comped = Task(e.id, e.title, e.description, true, e.createdAt)
        } else {
          comped = Task(e.id, e.title, e.description, false, e.createdAt)
        }
        tasks.save(comped)
      case None => NotFound(s"No entry for id=${id}")
    }
    Redirect(routes.TodoListController.list(userID)).withNewSession
  }

  def edit(userID: Int, id: Int) = Action { request =>
    tasks.findByID(id) match {
      case Some(e) => Ok(views.html.edit(request, e, userID))
      case None    => NotFound(s"Np entry for id=${id}")
    }
  }

  def update(userID: Int, id: Int) = Action { request =>
    (for {
      param       <- request.body.asFormUrlEncoded
      title       <- param.get("title").flatMap(_.headOption)
      description <- param.get("description").flatMap(_.headOption)
      isDone      <- param.get("isDone").flatMap(_.headOption)
    } yield {
      tasks.save(Task(id, title, description, isDone.toBoolean, null))
      Redirect("/todolist/" + userID + "/tasks").withNewSession
    }).getOrElse[Result](Redirect("/todolist/" + userID + "/tasks/" + id + "/edit"))
  }

  def delete(userID: Int, id: Int) = Action {
    tasks.findByID(id) match {
      case Some(e) =>
        tasks.delete(id)
        ids.delete(id)
      case None => NotFound(s"No entry for id=${id}")
    }
    Redirect(routes.TodoListController.list(userID)).withNewSession
  }

  def register(userID: Int) = Action { request =>
    Ok(views.html.register(request, userID)).withNewSession
  }

  def confirm(userID: Int) = Action { request =>
    (for {
      param       <- request.body.asFormUrlEncoded
      title       <- param.get("title").flatMap(_.headOption)
      description <- param.get("description").flatMap(_.headOption)
    } yield {
      tasks.save(Task(title, description, false))
      val last = tasks.list.last // 新たに追加したタスクは全タスクをid順に並べた時の末尾にある
      ids.save(userID, last.id)
      Redirect("/todolist/" + userID + "/tasks").withNewSession
    }).getOrElse[Result](Redirect("/todolist/" + userID + "/tasks/create"))
  }

  /**
    * 検索機能
    * @return
    */
  def extract(userID: Int) = Action { request =>
    (for {
      param  <- request.body.asFormUrlEncoded
      title  <- param.get("title").flatMap(_.headOption)
      isDone <- param.get("isDone").flatMap(_.headOption)
    } yield {
      val userName = users.getName(userID)
      val taskIDs  = ids.findByUserID(userID)
      if (title == "") {
        val entries = tasks.findByIsDone(isDone.toBoolean)
        Ok(views.html.list(request, entries, userID, userName, taskIDs))
      } else {
        val entries = tasks.search(title, isDone.toBoolean)
        Ok(views.html.list(request, entries, userID, userName, taskIDs))
      }
    }).getOrElse[Result](Redirect("/todolist/" + userID + "/tasks"))
  }

}
