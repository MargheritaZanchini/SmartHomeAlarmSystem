package smartHomeAlarm.actors

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.receptionist.{Receptionist, ServiceKey}
import org.apache.pekko.actor.typed.scaladsl.*
import smartHomeAlarm.actors.SmartHomeAlarmGuardian.Command.InputEntered

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

object UserInteraction:
  import smartHomeAlarm.smartHomeAlarmProtocol.*

  enum Command:
    case WaitInput()
    case SendInput(input: String)

  export Command.*
  val keyPadServiceKey = ServiceKey[Command]("keyPad")
  
  def apply(): Behavior[Command] = 
  Behaviors.setup { context =>
    context.self ! WaitInput()
    val router = context.spawn(Routers.group(SmartHomeAlarmGuardian.guardianServiceKey), "guardian")
    context.system.receptionist ! Receptionist.Register(keyPadServiceKey, context.self)
    active(router)
  }

  private def active(router: ActorRef[SmartHomeAlarmGuardian.Command]):Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        case WaitInput() =>

          startAsyncKeyboardReading(context)
          Behaviors.same
        case SendInput(input) =>
          router ! InputEntered(TryInput(input))
          context.self ! WaitInput()
          Behaviors.same


  private def startAsyncKeyboardReading(context: ActorContext[Command]): Unit =
    Future {
      val input = StdIn.readLine()
      if input != null then
        //invia il testo letto all'attore stesso in modo thread-safe
        context.self ! SendInput(input)
    }