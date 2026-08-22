package smartHomeAlarm.actors


import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.receptionist.{Receptionist, ServiceKey}
import smartHomeAlarm.actors.Alarm.alarmServiceKey
import smartHomeAlarm.actors.Sensor.Command
import smartHomeAlarm.actors.SmartHomeAlarmGuardian.Command.DetectedMovement
import smartHomeAlarm.smartHomeAlarmProtocol.sensorsType.*

import scala.util.Random
import java.util.UUID
import scala.concurrent.duration.{DurationInt, FiniteDuration}


object Sensor:
  import smartHomeAlarm.smartHomeAlarmProtocol.*
  //il sensore può ricevere due messaggi: waiting o detection
  enum Command:
    case Waiting(sensorID: UUID, sensorType: sensorsType)
    case Detection(sensorID: UUID, sensorType: sensorsType)
  export Command.*
  //intervallo minimo e massimo per la rilevazione di un movimento
  private val IntervalMinSeconds = 10
  private val IntervalMaxSeconds = 60

  val sensorServiceKey = ServiceKey[Command]("sensor")

  def apply(sensorType: sensorsType, sensorID: UUID):Behavior[Command] =
    Behaviors.setup { context =>
      Behaviors.withTimers { timers =>
        val sensorRouter = context.spawn(Routers.group(SmartHomeAlarmGuardian.guardianServiceKey), "guardian")
        context.system.receptionist ! Receptionist.Register(sensorServiceKey, context.self)
        //appena nasce si invia da solo il messaggio per iniziare ad aspettare (altrimenti non parte)
        context.self ! Waiting(sensorID, sensorType)
        active(timers, sensorRouter)
      }
    }

  private def active(timers: TimerScheduler[Command], router: ActorRef[SmartHomeAlarmGuardian.Command]
                    ): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        //caso in cui l'attore ottiene un messaggio Waiting
        case Waiting(sensorID, sensorType) =>
          
          val delay = Random.between(IntervalMinSeconds, IntervalMaxSeconds + 1).seconds
          timers.startSingleTimer(Detection(sensorID, sensorType), delay)
          //fa partire un timer di un delay random e al termine di questo invia a sé stesso (sensore) un messaggio Detection
          context.log.info("Motion detection in sensor {} in {}", sensorType, delay)
          Behaviors.same
        //caso in cui l'attore ottiene un messaggio Detection
        case Detection(sensorID, sensorType) =>
          context.log.info("Motion detected in sensor {}", sensorType)
          //risponde alla SmartHome che un movimento è stato rilevato
          router ! DetectedMovement(MotionDetected(sensorID))
          //manda un messaggio a sè stesso dicendosi di rimettersi in Waiting
          context.self ! Waiting(sensorID, sensorType)
          Behaviors.same
          
object SensorRouter:
  def apply(): Behavior[Unit] = Behaviors.setup: ctx =>
    val sensorList = List(PIRDoor, PIRLivingRoom, WindowSensor, WindowSensor)
    sensorList.foreach { sensorType =>
      val id = UUID.randomUUID()
      ctx.spawn(Sensor(sensorType, id), s"sensor-$sensorType-$id")
    }
    Behaviors.empty