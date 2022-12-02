// SPDX-License-Identifier: Apache-2.0
// Copyright © 2022 Florian Schmaus
package eu.geekplace.beanstalk.core.scala

import scala.collection.IterableOnce
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.util.{Try, Failure}

import java.net.Socket
import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedQueue

def happyEyeballs(targets: IterableOnce[InetAddress], port: Int, duration: Duration = 60.seconds)(implicit ec: ExecutionContext):
    (Try[Socket], ConcurrentLinkedQueue[Exception]) = {
  val promise = Promise[Socket]()
  val exceptions = new ConcurrentLinkedQueue[Exception]()

  for (
    target <- targets
  ) do Future {
    try {
      val socket = new Socket(target, port)
      if (!promise.trySuccess(socket)) socket.close()
    } catch {
      case e: Exception => exceptions.add(e)
    }
  }

  val socketResult = Try(Await.result(promise.future, duration)) recoverWith {
    case t: Throwable =>
      if (!promise.tryFailure(t)) {
        // In this case, the promise/future must have a socket already
        // set, which we now need to close to avoid leaking it.
        promise.future.value.get.get.close()
      }
      Failure(t)
  }

  (socketResult, exceptions)
}

def happyEyeballsWithVirtualThreads() = {
  //given ec: ExecutionContext = VirtualThread.executionContext
  import eu.geekplace.beanstalk.core.scala.VirtualThread.Implicits.executionContext

  //val dnsName = "badipv4.test.ipv6friday.org" 
  val dnsName = "badipv6.test.ipv6friday.org"
  val targets = InetAddress.getAllByName(dnsName)
  val (socketOption, exceptions) = happyEyeballs(targets, 80)
  println(s"$socketOption, $exceptions")
}
