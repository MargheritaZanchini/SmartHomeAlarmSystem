package smartHomeAlarm

import java.util.UUID

object smartHomeAlarmProtocol:

  enum sensorsType:
    case PIRDoor
    case PIRLivingRoom
    case WindowSensor

  final case class TryPin(pin: String)

  final case class MotionDetected(sensorID: UUID)

  final case class AlarmStarting()


  export sensorsType.*
