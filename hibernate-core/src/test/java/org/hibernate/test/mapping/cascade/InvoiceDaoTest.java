package org.hibernate.test.mapping.cascade;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Assert;
import org.junit.Test;


/**
 * Testing relationships between components: example invoice -> invoice line
 *
 * @author martin
 * @version $Id: InvoiceDaoTest.java,v 1.14 2017/07/31 09:03:02 lustig#his.de Exp $
 */
public class InvoiceDaoTest extends BaseCoreFunctionalTestCase{

    static String INVOICE_A = "Invoice A";
    static String INVOICELINE_A = "InvoiceLine A";
    static String INVOICELINE_B = "InvoiceLine B";
    
    @Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class[] { Invoice.class, InvoiceLine.class };
	}
    
    /**
     * Helping method: create invoice with two invoice lines
     * @return invoice
     */
    public Invoice createInvoiceWithTwoInvoiceLines(Session session) {
        InvoiceLine lineA = new InvoiceLine(INVOICELINE_A);
        InvoiceLine lineB = new InvoiceLine(INVOICELINE_B);

        Invoice invoice = new Invoice(INVOICE_A);
        invoice.addInvoiceLine(lineA);
        invoice.addInvoiceLine(lineB);
        
        session.persist(invoice);

        return invoice;
    }
    
    @Test
    public void testNoPersistCascadeOnUninitializedLazyAssociation() {
    	
    	try (Session session = openSession()) {
        	Transaction tx = session.beginTransaction();
            SessionStatistics stats = session.getStatistics();

            Invoice invoice = createInvoiceWithTwoInvoiceLines(session);
            session.flush();
            session.clear();

            // load invoice, invoiceLines should not be loaded
            invoice = (Invoice) session.get(Invoice.class, invoice.getId());
            Assert.assertEquals("Invoice lines should not be initialized while loading the invoice, because of the lazy association.", 1, stats.getEntityCount());


            // make change
            invoice.setName(invoice.getName() + " !");
            session.persist(invoice);

            session.flush();

            Assert.assertEquals(1, stats.getEntityCount()); // invoice lines should not be initialized
            tx.commit();
    	}
    }
}
