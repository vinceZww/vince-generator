import { GithubOutlined } from '@ant-design/icons';
import { DefaultFooter } from '@ant-design/pro-components';
import '@umijs/max';
import React from 'react';

const Footer: React.FC = () => {
  const defaultMessage = '程序员vince';
  const currentYear = new Date().getFullYear();
  return (
    <DefaultFooter
      style={{
        background: 'none',
      }}
      copyright={`${currentYear} ${defaultMessage}`}
      links={[
        {
          key: 'codeNav',
          title: '简书',
          href: 'https://www.jianshu.com/u/a5334b4b8dfe',
          blankTarget: true,
        },
        {
          key: 'Ant Design',
          title: '博客园',
          href: 'https://www.cnblogs.com/vince-zww',
          blankTarget: true,
        },
        {
          key: 'github',
          title: (
            <>
              <GithubOutlined /> vince源码
            </>
          ),
          href: 'https://github.com/vinceZww',
          blankTarget: true,
        },
      ]}
    />
  );
};
export default Footer;
