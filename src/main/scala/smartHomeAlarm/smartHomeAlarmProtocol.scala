package smartHomeAlarm


import java.util.UUID

object smartHomeAlarmProtocol:

  enum sensorsType:
    case PIRDoor
    case PIRLivingRoom
    case WindowSensor

  enum zones:
    case GroundFloor
    case UpperFloor
    case Garden
  
  enum modes(val activeZones: List[zones]):
    case FullMode extends modes(List(zones.GroundFloor, zones.UpperFloor, zones.Garden))
    case NightMode extends modes(List(zones.GroundFloor, zones.Garden))
    case DayMode extends modes(List(zones.Garden))

  final case class TryInput(input: String)

  final case class MotionDetected(sensorID: UUID)

  final case class AlarmStarting()


  export sensorsType.*
