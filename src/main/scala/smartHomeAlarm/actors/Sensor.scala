package smartHomeAlarm.actors


import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*

import java.util.UUID

object Sensor:
  import smartHomeAlarm.smartHomeAlarmProtocol.*
  enum Command:
    case Waiting(sensorID: UUID, replyTo: ActorRef[MotionDetected])

  //TODO aggiungere rifermento all'attore della control unit che riceve messaggio
  final case class MotionDetected(sensorID:UUID)

  def apply(sensorType: sensorsType):Behavior[Command]