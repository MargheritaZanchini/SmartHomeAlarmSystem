package smartHomeAlarm

import com.typesafe.config.ConfigFactory
import smartHomeAlarm.actors.{Alarm, Sensor, SensorRouter, SmartHomeAlarmGuardian, UserInteraction}
import org.apache.pekko.actor.typed.*
import smartHomeAlarm.smartHomeAlarmProtocol.sensorsType.*

import java.util.UUID

object SmartHomeAlarmApp:
//  @main def app(): Unit =
//
//    val system = ActorSystem(SmartHomeAlarmGuardian(), "SmartHomeSystem")
//    system.log.info("Smart Home Alarm System activated")

  
  @main def spawnAlarm(): Unit =
    val config = ConfigFactory.load("application.conf")
    val _ = ActorSystem[Alarm.Command](Alarm(), "Clustered-SmartHomeAlarmSystem")

  @main def spawnSensor(): Unit =
    val config = ConfigFactory.load("application.conf")
    val _ = ActorSystem[Unit](SensorRouter(), "Clustered-SmartHomeAlarmSystem", config)

  @main def spawnKeypad(): Unit =
    val config = ConfigFactory.load("application.conf")
    val _ = ActorSystem[UserInteraction.Command](UserInteraction(), "Clustered-SmartHomeAlarmSystem")
      
  @main def spawnControlUnit(): Unit =
    val config = ConfigFactory.load("application.conf")
    val _ = ActorSystem[SmartHomeAlarmGuardian.Command](SmartHomeAlarmGuardian(), "Clustered-SmartHomeAlarmSystem")