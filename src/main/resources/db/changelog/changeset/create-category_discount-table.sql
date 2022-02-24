databaseChangeLog:
  - changeSet:
      id: create-categories
      author: Denis Popov
      dbms: 'postgresql'
      changes:
        - createTable:
            tableName: category_discount
            columns:
              - column:
                  name: id
                  type: BIGINT
                  defaultValueSequenceNext: category_discount_id
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: category_id
                  type: BIGINT
                  constraints:
                    foreignKey: true
                    references: category(id)
              - column:
                  name: discount_id
                  type: BIGINT
                  constraints:
                    foreignKey: true
                    references: discounts(id)
