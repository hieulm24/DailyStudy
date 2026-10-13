package com.englishlearning.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

/**
 * Custom Physical Naming Strategy to preserve PascalCase table and column names
 * for the 5 pre-existing SQL Server financial tables without altering the snake_case
 * conventions used by other modules in the application.
 */
@Component
public class FinancialPhysicalNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    private static final Set<String> FINANCIAL_TABLES = Set.of(
            "financialaccounts",
            "financialcategories",
            "financialtransactions",
            "financialgoals",
            "financialgoalcontributions"
    );

    private static final Set<String> FINANCIAL_COLUMNS = Set.of(
            "id",
            "name",
            "description",
            "targetamount",
            "startdate",
            "targetdate",
            "status",
            "icon",
            "color",
            "accounttype",
            "initialbalance",
            "isactive",
            "isdefault",
            "type",
            "amount",
            "transactiondate",
            "note",
            "toaccountid",
            "accountid",
            "accountids",
            "categoryid",
            "goalid",
            "transactionid",
            "contributiondate",
            "month",
            "year",
            "expectedincome",
            "plannedamount",
            "createdat",
            "updatedat"
    );

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        if (logicalName != null) {
            String text = logicalName.getText();
            String lower = text.toLowerCase(Locale.ROOT);
            if (FINANCIAL_TABLES.contains(lower) || lower.startsWith("financial")) {
                return Identifier.toIdentifier(text, logicalName.isQuoted());
            }
        }
        return super.toPhysicalTableName(logicalName, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        if (logicalName != null) {
            String text = logicalName.getText();
            // Preserve PascalCase column names for financial entities (which do not contain underscores)
            if (!text.contains("_")) {
                String lower = text.toLowerCase(Locale.ROOT);
                if (FINANCIAL_COLUMNS.contains(lower)) {
                    return Identifier.toIdentifier(text, logicalName.isQuoted());
                }
            }
        }
        return super.toPhysicalColumnName(logicalName, jdbcEnvironment);
    }
}
