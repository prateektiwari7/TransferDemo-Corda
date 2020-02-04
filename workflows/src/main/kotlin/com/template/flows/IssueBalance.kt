package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.MergeState_Contract
import com.template.states.MergeState_State
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class IssueBalance(val balance: Int,val party: Party) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() : SignedTransaction{
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val outputstate = MergeState_State(balance,party)

        val command = Command(MergeState_Contract.Commands.Consume(), listOf(party).map { it.owningKey })

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(outputstate, MergeState_Contract.ID)
                .addCommand(command)


        txBuilder.verify(serviceHub)
        val tx = serviceHub.signInitialTransaction(txBuilder)

        val sessions = initiateFlow(party)
        val stx = subFlow(CollectSignaturesFlow(tx, listOf(sessions)))
        return subFlow(FinalityFlow(stx, listOf(sessions)))


    }
}

@InitiatedBy(IssueBalance::class)
class IssueBalance_Responder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data


            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        return subFlow(ReceiveFinalityFlow(counterpartySession, txWeJustSignedId.id))
    }
}


@InitiatingFlow
@StartableByRPC
class Transfer(val balance: Int,val party: Party) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() : SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val otheroutputstate= MergeState_State(balance,party)

        val command = Command(MergeState_Contract.Commands.Consume(), listOf(party).map { it.owningKey })

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(otheroutputstate, MergeState_Contract.ID)
                .addCommand(command)

        txBuilder.verify(serviceHub)
        val tx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(TransferMoney(balance,party))
        val sessions = initiateFlow(party)
        val stx = subFlow(CollectSignaturesFlow(tx, listOf(sessions)))
        return subFlow(FinalityFlow(stx, listOf(sessions)))



    }
}

@InitiatedBy(Transfer::class)
class Transfer_Responder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data

            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        return subFlow(ReceiveFinalityFlow(counterpartySession, txWeJustSignedId.id))
    }
}




@InitiatingFlow
class TransferMoney(val balance: Int,val party: Party) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() : SignedTransaction{

        val statelist = serviceHub.vaultService.queryBy(MergeState_State::class.java).states
        val vaultstate = statelist.get(statelist.size-1).state.data
        val InState = serviceHub.toStateAndRef<MergeState_State>(statelist.get(statelist.size-1).ref)

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val selfoutputstate = MergeState_State(vaultstate.balance-balance,vaultstate.owner)

        val command = Command(MergeState_Contract.Commands.Consume(), listOf(party).map { it.owningKey })

        val txBuilder = TransactionBuilder(notary)
                .addInputState(InState)
                .addOutputState(selfoutputstate, MergeState_Contract.ID)
                .addCommand(command)

        txBuilder.verify(serviceHub)
        val tx = serviceHub.signInitialTransaction(txBuilder)

        val sessions = initiateFlow(party)
        val stx = subFlow(CollectSignaturesFlow(tx, listOf(sessions)))
        return subFlow(FinalityFlow(stx, listOf(sessions)))



    }
}

@InitiatedBy(TransferMoney::class)
class TransferMoney_Responder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data

            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        return subFlow(ReceiveFinalityFlow(counterpartySession, txWeJustSignedId.id))
    }
}








