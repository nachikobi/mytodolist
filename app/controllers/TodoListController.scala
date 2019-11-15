package controllers

import javax.inject.{Inject, Singleton}
import models.{Task, Tasks}
import play.api.mvc.{AbstractController, ControllerComponents, Result}

/**
  * TodoListコントローラ
  */
@Singleton
class TodoListController @Inject()(tasks: Tasks)(cc: ControllerComponents) extends AbstractController(cc) {

  /**
    * インデックスページを表示
    */
  def index = Action { implicit request =>
    // 200 OK ステータスで app/views/index.scala.html をレンダリングする
    Ok(views.html.index("Welcome to Play application!"))
  }

  def list = Action { request =>
    val entries = tasks.list
    Ok(views.html.list(entries))
  }

  def entry(id: Int) = Action {
    tasks.findByID(id) match {
      case Some(e) => Ok(views.html.entry(e))
      case None    => NotFound(s"No entry for id=${id}")
    }
  }

  def comp(id: Int) = Action {
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
    Redirect(routes.TodoListController.list).withNewSession
  }

  def edit(id: Int) = TODO

  def update(id: Int) = TODO

  def delete(id: Int) = TODO

  def register = Action { request =>
    Ok(views.html.register(request)).withNewSession
  }

  def confirm = Action { request =>
    (for {
      param       <- request.body.asFormUrlEncoded
      title       <- param.get("title").flatMap(_.headOption)
      description <- param.get("description").flatMap(_.headOption)
    } yield {
      tasks.save(Task(title, description, false))
      Redirect("/todolist/tasks").withNewSession
    }).getOrElse[Result](Redirect("/todolist/tasks/create"))
  }

}
