package com.github.nebtrx

import cats.instances.string._
import cats.instances.vector._
import monix.eval.{Fiber, MVar, Task}
import monix.execution.schedulers.SchedulerService
import monix.execution.{Cancelable, CancelableFuture, Scheduler}

import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global
//import cats.implicits._
import cats.syntax.traverse._
import cats.syntax.semigroup._



object Main extends App {

//  type Logged[A] = Writer[Vector[String], A]

  type LogState[A] = MVar[Vector[A]]

  def periodically[A](initialDelay: FiniteDuration, delay: FiniteDuration)
                     (scheduledTask: Task[A]): Task[Either[CancelableFuture[A], A]]= {

    lazy val io: SchedulerService = Scheduler.io(name="my-io")

    // On execution, we have the scheduler and
    // the callback injected ;-)
    val task: Task[Either[CancelableFuture[A], A]] = Task.create { (scheduler, callback) =>
      val cancelable: Cancelable =
        scheduler.scheduleAtFixedRate(initialDelay, delay) {
          callback(scheduledTask.coeval.run.toTry)
        }

      // We must return something that can
      // cancel the async computation
      cancelable
    }
//    val gg: Try[Either[CancelableFuture[A], A]] = task.coeval.run.toTry
//    task.doOnFinish((_: Option[Throwable]) => Task.now{println("termino")})
    task
  }


//  def periodically[A](initialDelay: FiniteDuration, delay: FiniteDuration)
//                     (f: => A): Task[A] = {
//
//    lazy val io: SchedulerService = Scheduler.io(name="my-io")
//
//    // On execution, we have the scheduler and
//    // the callback injected ;-)
//    val task: Task[A] = Task.create { (scheduler, callback) =>
//      val cancelable: Cancelable =
//
//
//        scheduler.scheduleAtFixedRate(initialDelay, delay) {
//          callback(Try(f))
//        }
//
//      // We must return something that can
//      // cancel the async computation
//      cancelable
//    }
//  }


  println("Hello " |+| "Cats!")

  val state: Task[LogState[String]] = MVar(Vector.empty)

  val taskBuilder: (String, Task[LogState[String]]) => Task[LogState[String]] = (id: String, t: Task[LogState[String]]) => {
    for {
      mv <- t
      current <- mv.take
      logEntry:String = s"[${System.currentTimeMillis()}] => $id"
      _ <- mv.put(current |+| Vector[String](logEntry))
    } yield mv
  }


  val list = (1 to 5).map(id => {
    periodically(id.seconds, 1.seconds) (taskBuilder(id.toString, state))
  })

  var ts = Task.sequence(list).timeout(30.seconds)
  ts.runOnComplete(e => println(s"SUMMARY $e"))
  ts.runAsync

//
//  val timedOut = Task.sequence(list).runAsync.timeout(3.seconds)
//
//  timedOut.runOnComplete(r => println(r))

//  list.map(_.fork)



//  periodically(2.seconds, 3.seconds) ((_: Any) => {
//    val x = 4*45355*345355*543535
//    println(s"printing $x")
//    x
//  }).runAsync.foreach(println)
//
  println("mande a ejecutar")

//  val task = Task.defer {
//    Task.now { println("Effect"); "Hello!" }
//  }.runAsync.foreach(println)


  scala.io.StdIn.readLine()
}
