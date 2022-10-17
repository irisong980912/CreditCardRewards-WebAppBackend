package com.xiaoqigao.creditcardrewards.DAO;

import com.xiaoqigao.creditcardrewards.model.Transaction;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface TestTransactionDAO {

    @Insert("INSERT INTO `transaction` (transaction_name, post_year, post_month, post_day, merchant_code, amount_cents) " +
            "VALUES (#{transactionName}, #{postYear}, #{postMonth}, #{postDay}, #{merchantCode}, #{amountCents})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    void insert(Transaction transaction);

    @Select("SELECT * FROM `transaction` WHERE post_year=#{postYear} AND post_month=#{postMonth}")
    List<Transaction> selectByYearMonth(@Param("postYear") String postYear, @Param("postMonth") String postMonth);

    @Select("SELECT * FROM `transaction` WHERE id=#{id}")
    Transaction selectOneByID(@Param("id") int id);

    @Select("SELECT * FROM `transaction` WHERE transaction_name=#{transactionName}")
    List<Transaction> selectByTransactionName(@Param("transactionName") String transactionName);

    @Delete("DELETE FROM `transaction`")
    void deleteAll();

}