package com.example.backend.config;

import com.example.backend.entity.*;
import com.example.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("系统管理员");
            adminRepository.save(admin);
            System.out.println("默认管理员账号创建成功: admin / admin123");
        }

        if (userRepository.count() == 0) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setNickname("测试用户");
            user.setPhone("13800138000");
            userRepository.save(user);
            System.out.println("默认测试用户创建成功: user / user123");
        }

        if (categoryRepository.count() == 0) {
            createCategory(null, "新疆美食", "新疆特色美食、特产小吃", "http://img.alicdn.com/img/i1/2206691970950/O1CN015IxOy11It9FYJ0l61_!!2206691970950.jpg");
            createCategory(null, "节日礼品", "节日礼盒、伴手礼", "https://qiniu.wodsy.com/file/20250109/173639240997256544.jpg");
            createCategory(null, "珠宝玉器", "和田玉、宝石、首饰", "https://img.alicdn.com/bao/uploaded/i1/2209938020780/O1CN01J1plMf1HdHgbMAOXQ_!!2209938020780.jpg");
            createCategory(null, "时尚配饰", "民族风饰品、挂件", "http://img.alicdn.com/img/i1/181900190/O1CN01h57WQx1DH4NVAAqbd_!!4611686018427384734-0-saturn_solar.jpg");
            createCategory(null, "雕刻塑造", "木雕、玉雕、泥塑", "http://img.alicdn.com/img/i2/111579494/O1CN01ucvV2o2K0JHJjMwNU_!!4611686018427384166-0-saturn_solar.jpg");

            System.out.println("默认分类创建成功");
        }

        if (brandRepository.count() == 0) {
            createBrand("和田玉坊", "新疆和田玉知名品牌", "https://n.sinaimg.cn/sinakd10116/200/w900h900/20221205/d051-68a7acd80d70b93674a515dc2d93226c.jpg", "https://www.hetian-yufang.com", "中国新疆");
            createBrand("西域果园", "新疆特产龙头企业", "https://so1.360tres.com/t014f7f57dcd7c1379a.jpg", "https://www.xiyu-guoyuan.com", "中国新疆");
            createBrand("艾德莱斯", "新疆民族服饰品牌", "https://pic.nximg.cn/file/20250408/33157675_195131305912_2.jpg", "https://www.adilaisi.com", "中国新疆");
            System.out.println("默认品牌创建成功");
        }

        if (productRepository.count() == 0) {
            Long hetianId = brandRepository.findByNameContaining("和田玉坊").get(0).getId();
            Long xiyuId = brandRepository.findByNameContaining("西域果园").get(0).getId();
            Long adilaisiId = brandRepository.findByNameContaining("艾德莱斯").get(0).getId();

            Long foodId = categoryRepository.findByNameContaining("新疆美食").get(0).getId();
            Long jewelryId = categoryRepository.findByNameContaining("珠宝玉器").get(0).getId();
            Long accessoriesId = categoryRepository.findByNameContaining("时尚配饰").get(0).getId();

            createProduct("特级葡萄干礼盒", "精选新疆吐鲁番葡萄干，颗粒饱满", foodId, xiyuId,
                    new BigDecimal("128.00"), new BigDecimal("168.00"), new BigDecimal("88.00"), 200, 20,
                    "https://cbu01.alicdn.com/img/ibank/O1CN01Qkb6Tf22Ot8MimQb3_!!2207738197111-0-cib.jpg", "盒", 1, 1, 1, 1, new BigDecimal("0.50"));

            createProduct("和田玉手镯", "天然和田白玉手镯，温润细腻", jewelryId, hetianId,
                    new BigDecimal("2999.00"), new BigDecimal("3999.00"), new BigDecimal("2200.00"), 50, 5,
                    "https://img.alicdn.com/bao/uploaded/i1/2209938020780/O1CN01J1plMf1HdHgbMAOXQ_!!2209938020780.jpg", "只", 1, 1, 1, 1, new BigDecimal("0.15"));

            createProduct("艾德莱斯绸围巾", "新疆传统工艺，精美图案", accessoriesId, adilaisiId,
                    new BigDecimal("198.00"), new BigDecimal("268.00"), new BigDecimal("138.00"), 100, 10,
                    "http://img.alicdn.com/img/O1CN01gEP9p21YwO2UNwYyC_!!2218968103123.jpg", "条", 1, 1, 1, 1, new BigDecimal("0.05"));

            createProduct("巴旦木礼盒", "新疆喀什巴旦木，香脆可口", foodId, xiyuId,
                    new BigDecimal("98.00"), new BigDecimal("128.00"), new BigDecimal("68.00"), 150, 15,
                    "http://img.alicdn.com/img/O1CN01BxSwId2BLXBjLODvs_!!2219162938322.jpg", "盒", 1, 1, 1, 1, new BigDecimal("0.40"));

            System.out.println("默认商品创建成功");
        }
    }

    private void createCategory(Long parentId, String name, String description, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setParentId(parentId);
        category.setIcon(icon);
        category.setSort(0);
        category.setStatus(1);
        categoryRepository.save(category);
    }

    private void createBrand(String name, String description, String logo, String website, String country) {
        Brand brand = new Brand();
        brand.setName(name);
        brand.setDescription(description);
        brand.setLogo(logo);
        brand.setWebsite(website);
        brand.setCountry(country);
        brand.setSort(0);
        brand.setStatus(1);
        brandRepository.save(brand);
    }

    private void createProduct(String name, String description, Long categoryId, Long brandId,
                               BigDecimal price, BigDecimal originalPrice, BigDecimal costPrice,
                               Integer stock, Integer lowStock, String pic, String unit,
                               Integer status, Integer isPublish, Integer isNew, Integer isHot, BigDecimal weight) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setContent(description + " - 详细描述");
        product.setCategoryId(categoryId);
        product.setBrandId(brandId);
        product.setPrice(price);
        product.setOriginalPrice(originalPrice);
        product.setCostPrice(costPrice);
        product.setStock(stock);
        product.setLowStock(lowStock);
        product.setPic(pic);
        product.setUnit(unit);
        product.setStatus(status);
        product.setIsPublish(isPublish);
        product.setIsNew(isNew);
        product.setIsHot(isHot);
        product.setWeight(weight);
        productRepository.save(product);
    }
}