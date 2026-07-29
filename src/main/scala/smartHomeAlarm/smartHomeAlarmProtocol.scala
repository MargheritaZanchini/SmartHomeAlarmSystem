package smartHomeAlarm

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


  export sensorsType.*
  export cuStates.*