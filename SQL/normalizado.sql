select distinct
       lei.arquivo,
	   lei.estado,
	   segmento.nome,
	   segmento.descricao_segmento,
	   segmento.efeito,
	   segmento.versao,
	   segmento.observacoes,
	   cest.codigo,
	   item.descricao,
       item.op_interna,
	   acordo.tipo as EstadosDestino,
	   acordo.acordo,
       eali.descricao as Aliquota,
	   eali.especificacao as EspeficacaoAli,
	   aliquota.descricao as DescricaoAli,
	   aliquota.valor as ValorAliquota,
       emva.descricao as DescricaoMVA,
	   emva.especificacao as EspeficacaoMVA,
	   mva.descricao as DescricaoMva,
       mva.valor as ValorMva,
	   epfc.especificacao as DescricaoPFC,
       pfc.valor as ValorPFC
  from segmento
  join lei on lei.id = segmento.lei_id
  join item on item.segmento_id = segmento.id
  join cest on cest.id = item.cest_id
  join aliquota on aliquota.item_id = item.id
  left join mva on mva.item_id = item.id 
  left join pfc on pfc.item_id = item.id 
  left join especificacao eali on eali.item_id = item.id  and eali.tipo = aliquota.tipo
  left join especificacao emva on emva.item_id = item.id  and emva.tipo = mva.tipo 
  left join especificacao epfc on epfc.item_id = item.id  and epfc.tipo = pfc.tipo   
  left join acordo on acordo.item_id = item.id		
where lei.ultima_versao is true
order by lei.arquivo, lei.estado, cest.codigo, aliquota.descricao
