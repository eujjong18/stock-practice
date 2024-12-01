package com.example.stock;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import com.example.stock.service.StockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class StockApplicationTests {

	@Autowired
	private StockService stockService;

	@Autowired
	private StockRepository stockRepository;

	@BeforeEach
	public void before(){
		stockRepository.saveAndFlush(new Stock(1L, 100L));
	}

	@AfterEach
	public void after(){
		stockRepository.deleteAll();
	}

	@Test
	public void 재고감소(){
		stockService.decrease(1L, 1L);

		// stock id: 1 -> 99개 (100-1)
		Stock stock = stockRepository.findById(1L).orElseThrow();

		assertEquals(99, stock.getQuantity());
	}

	@Test
	public void 동시에_100개의_요청() throws InterruptedException {
		int threadCount = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		CountDownLatch latch = new CountDownLatch(threadCount);

		for(int i = 0; i < threadCount; i++){
			executorService.submit(() -> {
				try {
					stockService.decrease(1L, 1L);
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		Stock stock = stockRepository.findById(1L).orElseThrow();
		// 예상값: 100 - (1*100) = 0
		assertEquals(0, stock.getQuantity());
	}

}
