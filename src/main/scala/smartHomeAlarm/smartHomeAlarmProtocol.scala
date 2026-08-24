package smartHomeAlarm


import smartHomeAlarm.smartHomeAlarmProtocol.zones.{Garden, GroundFloor, UpperFloor}

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

  //vengono definiti degli UUID fissi uguali per tutti
  val UUIDDoor: UUID = UUID.fromString("11111111-1111-1111-1111-111111111111")
  val UUIDLivingRoom: UUID = UUID.fromString("22222222-2222-2222-2222-222222222222")
  val UUIDWindow1: UUID = UUID.fromString("33333333-3333-3333-3333-333333333333")
  val UUIDWindow2: UUID = UUID.fromString("44444444-4444-4444-4444-444444444444")
  
  
  export sensorsType.*
