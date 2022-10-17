package com.xiaoqigao.creditcardrewards.DAO;

import com.xiaoqigao.creditcardrewards.model.Transaction;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Data access object that provides an abstract interface to `rewards` database
 * Separates a data resource's client interface from its data access mechanisms.
 * This isolation satisfy the "single responsibility" requirement.
 */
@Mapper
@Repository
public interface TransactionDAO {

    @Insert("INSERT INTO `transaction` (transaction_name, post_year, post_month, post_day, merchant_code, amount_cents) " +
            "VALUES (#{transactionName}, #{postYear}, #{postMonth}, #{postDay}, #{merchantCode}, #{amountCents})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    void insert(Transaction transaction);

    @Select("SELECT * FROM `transaction` WHERE post_year=#{postYear} AND post_month=#{postMonth}")
    List<Transaction> selectByYearMonth(@Param("postYear") String postYear, @Param("postMonth") String postMonth);

    @Select("SELECT * FROM `transaction` WHERE transaction_name=#{transactionName}")
    List<Transaction> selectByTransactionName(@Param("transactionName") String transactionName);

}
