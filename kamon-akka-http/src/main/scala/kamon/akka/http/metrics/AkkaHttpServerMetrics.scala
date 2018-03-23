/*
 * =========================================================================================
 * Copyright © 2013-2017 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package kamon.akka.http.metrics

import akka.http.scaladsl.model.HttpResponse
import kamon.Kamon
import kamon.akka.http.AkkaHttpExtension
import kamon.metric.EntityRecorderFactoryCompanion
import kamon.metric.instrument.InstrumentFactory
import kamon.util.http.HttpServerMetrics

class AkkaHttpServerMetrics(instrumentFactory: InstrumentFactory) extends HttpServerMetrics(instrumentFactory) {
  val requestActive = minMaxCounter("request-active")
  val connectionOpen = minMaxCounter("connection-open")

  def recordRequest() = requestActive.increment()

  def recordResponse(response: HttpResponse, traceName: String, traceTags: Map[String, String]): Unit = {
    requestActive.decrement()

    val entity = Kamon.metrics.entity(AkkaHttpServerMetrics, AkkaHttpExtension.ServerLibraryName, traceTags)
    val statusCode = response.status.intValue.toString

    entity
      .counter(statusCode)
      .increment()

    entity
      .counter(traceName + "_" + statusCode)
      .increment()
  }

  def recordConnectionOpened(): Unit = connectionOpen.increment()

  def recordConnectionClosed(): Unit = connectionOpen.decrement()
}

object AkkaHttpServerMetrics extends EntityRecorderFactoryCompanion[AkkaHttpServerMetrics]("http-server", new AkkaHttpServerMetrics(_))
