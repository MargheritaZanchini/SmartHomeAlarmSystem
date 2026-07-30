package smartHomeAlarm.actors


import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*

import scala.util.Random
import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object Sensor:
  import smartHomeAlarm.smartHomeAlarmProtocol.*
  //il sensore può ricevere due messaggi: waiting o detection
  enum Command:
    case Waiting(sensorID: UUID, replyTo: ActorRef[MotionDetected])
    case Detection(sensorID: UUID, replyTo: ActorRef[MotionDetected])
  export Command.*
  //intervallo minimo e massimo per la rilevazione di un movimento
  val intervalMin = 10
  val intervalMax = 60

  def apply(sensorType: sensorsType):Behavior[Command] =
    Behaviors.withTimers: timers =>
      active(timers)

  private def active(timers: TimerScheduler[Command]
                    ): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        //caso in cui l'attore ottiene un messaggio Waiting
        case Waiting(sensorID, replyTo) =>
          val delay = Random.between(intervalMin, intervalMax)
          //fa partire un timer di un delay random e al termine di questo invia a sé stesso (sensore) un messaggio Detection
          timers.startSingleTimer(Detection(sensorID, replyTo), FiniteDuration(delay, TimeUnit.SECONDS))
          context.log.info("Motion detection in sensor {} in {} seconds", sensorID, delay)
          Behaviors.same
        //caso in cui l'attore ottiene un messaggio Detection
        case Detection(sensorID, replyTo) =>
          context.log.info("Motion detected in sensor {}", sensorID)
          //risponde alla SmartHome che un movimento è stato rilevato
          replyTo ! MotionDetected(sensorID)
          //manda un messaggio a sè stesso dicendosi di rimettersi in Waiting
          context.self ! Waiting(sensorID, replyTo)
          Behaviors.same