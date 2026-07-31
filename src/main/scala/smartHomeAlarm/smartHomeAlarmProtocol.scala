package smartHomeAlarm

import java.util.UUID

object smartHomeAlarmProtocol:

  enum sensorsType:
    case PIRDoar
    case PIRLivingRoom
    case WindowSensor

  enum cuStates:
    case Disarmed
    case ExitDelay
    case Armed
    case EntryDelay
    case Emergency

  final case class MotionDetected(sensorID: UUID)

  export sensorsType.*
  export cuStates.*