package smartHomeAlarm.actors


import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*

import scala.util.Random
import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object Sensor:
  import smartHomeAlarm.smartHomeAlarmProtocol.*
  enum Command:
    case Waiting(sensorID: UUID, replyTo: ActorRef[MotionDetected])
    case Detection(sensorID: UUID, replyTo: ActorRef[MotionDetected])
  export Command.*
  val intervalMin = 10
  val intervalMax = 60

  final case class MotionDetected(sensorID:UUID)

  def apply(sensorType: sensorsType):Behavior[Command] =
    Behaviors.withTimers: timers =>
      active(timers)



  private def active(timers: TimerScheduler[Command]
                    ): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        case Waiting(sensorID, replyTo) =>
          val delay = Random.between(intervalMin, intervalMax)
          timers.startSingleTimer(Detection(sensorID, replyTo), FiniteDuration(delay, TimeUnit.SECONDS))
          context.log.info("Motion detection in sensor {} in {} seconds", sensorID, delay)
          active(timers)
        case Detection(sensorID, replyTo) =>
          context.log.info("Motion detected in sensor{}", sensorID)
          replyTo ! MotionDetected(sensorID)
          active(timers)






