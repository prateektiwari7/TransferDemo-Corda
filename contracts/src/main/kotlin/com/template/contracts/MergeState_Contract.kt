package com.template.contracts

import com.template.states.MergeState_State
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class MergeState_Contract : net.corda.core.contracts.Contract {


    companion object {
        const val ID = "com.template.contracts.MergeState_Contract"
    }

    override fun verify(tx: LedgerTransaction) {


        val command = tx.commands.requireSingleCommand<MergeState_Contract.Commands>()


        when (command.value) {

            is MergeState_Contract.Commands.Move -> requireThat {

                requireThat {


                    // Don't need to check anything else, as if outputs.size == 1 then the output is equal to
                    // the input ignoring the owner field due to the grouping.

                    val out = tx.outputsOfType<MergeState_State>().single()
                    val in1 = tx.inputsOfType<MergeState_State>().single()

                    "Dont make it on same state" using (out.owner != in1.owner)
                }

            }

            is MergeState_Contract.Commands.Issue -> {

                requireThat {

                    "No inputs should be consumed when issuing an IOU." using (tx.inputs.isEmpty())
                    "Only one output state should be created." using (tx.outputs.size == 1)

                    // val out = tx.outputsOfType<MergeState_State>().single()
                    // "The owner and creator can't be same" using ( out.owner != )


                }
            }

            is MergeState_Contract.Commands.Merge -> {

                requireThat {


                    "Only one output state should be created." using (tx.outputs.size == 1)

                    // val out = tx.outputsOfType<MergeState_State>().single()
                    // "The owner and creator can't be same" using ( out.owner != )


                }
            }



            is MergeState_Contract.Commands.Consume -> {

                requireThat {


                    "Only one output state should be created." using (tx.outputs.size == 1 || tx.outputs.size == 2)

                    // val out = tx.outputsOfType<MergeState_State>().single()
                    // "The owner and creator can't be same" using ( out.owner != )


                }
            }


        }
    }


    interface Commands : CommandData {


        class Move : TypeOnlyCommandData(), MergeState_Contract.Commands

        class Issue : TypeOnlyCommandData(), MergeState_Contract.Commands

        class Merge : TypeOnlyCommandData(), MergeState_Contract.Commands

        class Consume: TypeOnlyCommandData() , MergeState_Contract.Commands
    }
}
