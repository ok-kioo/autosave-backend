ALTER TABLE users
    ADD CONSTRAINT users_plan_contract_fk FOREIGN KEY (plan_contract_id) REFERENCES plan_contract(id);