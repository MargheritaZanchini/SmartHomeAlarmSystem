package smartHomeAlarm.actors

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*


import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

object UserInteraction:
  import smartHomeAlarm.smartHomeAlarmProtocol.*

  enum Command:
    case WaitInput()
    case SendInput(input: String)

  export Command.*

  def apply(replyTo: ActorRef[TryInput]): Behavior[Command] = 
  Behaviors.setup { context =>
    context.self ! WaitInput()
    active(replyTo)
  }

  private def active(replyTo: ActorRef[TryInput]):Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        case WaitInput() =>

          startAsyncKeyboardReading(context)
          Behaviors.same
        case SendInput(input) =>
          replyTo ! TryInput(input)
          context.self ! WaitInput()
          Behaviors.same


  def startAsyncKeyboardReading(context: ActorContext[Command]): Unit =
    Future {
      val input = StdIn.readLine()
      if input != null then
        // Invia il testo letto all'attore stesso in modo thread-safe
        context.self ! SendInput(input)
    }