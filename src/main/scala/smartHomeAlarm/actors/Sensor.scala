package smartHomeAlarm.actors


import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*

import scala.util.Random
import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object Sensor:
  import smartHomeAlarm.smartHomeAlarmProtocol.*
  //il sensore può ricevere due messaggi: waiting o detection
  enum Command:
    case Waiting(sensorID: UUID, replyTo: ActorRef[MotionDetected])
    case Detection(sensorID: UUID, replyTo: ActorRef[MotionDetected])
  export Command.*
  //intervallo minimo e massimo per la rilevazione di un movimento
  private val IntervalMinSeconds = 10
  private val IntervalMaxSeconds = 60

  def apply(sensorType: sensorsType, sensorID: UUID, replyTo: ActorRef[MotionDetected]):Behavior[Command] =
    Behaviors.setup { context =>
      Behaviors.withTimers { timers =>
        //appena nasce si invia da solo il messaggio per iniziare ad aspettare (altrimenti non parte)
        context.self ! Waiting(sensorID, replyTo)
        active(timers)
      }
    }

  private def active(timers: TimerScheduler[Command]
                    ): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        //caso in cui l'attore ottiene un messaggio Waiting
        case Waiting(sensorID, replyTo) =>
          
          val delay = Random.between(IntervalMinSeconds, IntervalMaxSeconds + 1).seconds
          timers.startSingleTimer(Detection(sensorID, replyTo), delay)
          //fa partire un timer di un delay random e al termine di questo invia a sé stesso (sensore) un messaggio Detection
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