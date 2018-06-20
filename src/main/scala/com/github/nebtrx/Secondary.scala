package com.github.nebtrx

import cats.instances.string._
import cats.instances.vector._
import com.github.nebtrx.Secondary.state
import monix.eval.{MVar, Task}
import monix.execution.Scheduler.Implicits.global
import monix.execution.schedulers.SchedulerService
import monix.execution.{Cancelable, CancelableFuture, Scheduler}

import scala.collection.immutable
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
//import cats.implicits._
import cats.syntax.semigroup._
import cats.syntax.parallel._


object Secondary extends App {

//  type Logged[A] = Writer[Vector[String], A]

  type LogState[A] = MVar[Vector[A]]


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

  val taskBuilder: (String, LogState[String]) => Task[LogState[String]] = (id: String, mv: LogState[String]) => {
    for {
      current <- mv.take
      logEntry:String = s"[${System.currentTimeMillis()}] => $id"
      _ <- mv.put(current |+| Vector[String](logEntry))
      updated <- mv.take
//      _ = println(s"AAA ${Vector[String](logEntry) |+| Vector[String](logEntry)}")
      _ = println(s"[${Thread.currentThread.getName}] $logEntry [MVAR:  $updated]")
    } yield mv
  }




//  val list: Seq[Task[LogState[String]]] = (1 to 5).map((id:Int) => {
//    taskBuilder(id.toString, state)
//  })

  var ts: Task[Vector[String]] = for {
    ms <- MVar(Vector.empty[String])
    tasks = (1 to 5).map((id:Int) => { taskBuilder(id.toString, ms)})
    _ <- Task.sequence(tasks)
    s <- ms.read
  } yield s

//  val finalFn: Try[Seq[LogState[String]]] => Unit = {
//    case Success(l) => l.map(ls => ls.read.flatMap( v => {
//      println("VECTOOOR")
//      println(v)
//      Task.unit
//    }))
//    case Failure(e) => println(e)
//  }

//  ts.runOnComplete(finalFn)
//  println("ABOUT TO RUN")
  ts.runAsync.foreach(println)


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

//  val task = Task.defer {
//    Task.now { println("Effect"); "Hello!" }
//  }.runAsync.foreach(println)


  scala.io.StdIn.readLine()
}
