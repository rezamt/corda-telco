package com.telco.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.time.LocalDate
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for InvoiceState.
 */
object InvoiceSchema

/**
 * An InvoiceState schema.
 */
object InvoiceSchemaV1 : MappedSchema(
        schemaFamily = InvoiceSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentInvoice::class.java)) {
    @Entity
    @Table(name = "telco_invoice")   // namespace: telco _ table: invoice
    class PersistentInvoice(
            @Column(name = "InvoiceID")
            var invoiceID: UUID,

            @Column(name = "serviceProvider")
            var serviceProvider: String,

            @Column(name = "customerID")
            var customerID: String,

            @Column(name = "billingAccountID")
            var billingAccountID: String,

            @Column(name = "serviceType")
            var serviceType: String,

            @Column(name = "issueDate")
            var issueDate: LocalDate,
            @Column(name = "dueDate")
            var dueDate: LocalDate,

            @Column(name = "amount")
            var amount: Double,

            @Column(name = "status") // PAID, UNPAID, OVERDUE
            var status: String,


            @Column(name = "linear_id")
            var linearId: UUID
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this(UUID.randomUUID(), "", "","", "", LocalDate.MIN, LocalDate.MIN, 0.0, "", UUID.randomUUID())
    }
}