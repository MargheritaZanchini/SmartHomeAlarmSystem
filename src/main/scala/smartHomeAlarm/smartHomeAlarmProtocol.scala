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

  final case class TryPin(pin: String)

  final case class MotionDetected(sensorID: UUID)

  final case class AlarmStarting()


  export sensorsType.*
  export cuStates.*
