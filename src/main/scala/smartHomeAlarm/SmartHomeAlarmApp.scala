package smartHomeAlarm

import smartHomeAlarm.actors.SmartHomeAlarmGuardian

import org.apache.pekko.actor.typed.*

object SmartHomeAlarmApp:
  @main def app(): Unit =
    
    val system = ActorSystem(SmartHomeAlarmGuardian(), "SmartHomeSystem")
    system.log.info("Smart Home Alarm System activated")
